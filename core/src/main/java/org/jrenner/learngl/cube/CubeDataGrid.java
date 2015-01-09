package org.jrenner.learngl.cube;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import org.jetbrains.annotations.NotNull;
import org.jrenner.learngl.Main;
import org.jrenner.learngl.gameworld.Chunk;
import org.jrenner.learngl.gameworld.CubeData;
import org.jrenner.learngl.gameworld.CubeTypes;

import java.util.Iterator;

public class CubeDataGrid implements Iterable<CubeData> {
	private static final int chunkSize = Chunk.OBJECT$.getChunkSize();
	public static int width = chunkSize;
	public static int height = chunkSize;
	public static int depth = chunkSize;
	public boolean dirty = true;
	public int numElements;
	public CubeData[][][] grid = new CubeData[chunkSize][chunkSize][chunkSize];

	/** the world coordinates projection of local coords (0f,0f,0f)
	 * i.e. the 'left', 'bottom', 'front' corner of the box in world space */
	public Vector3 origin = new Vector3();

	/** center of the box the CDG represents in world coordinates */
	public Vector3 center = new Vector3();

	/** (width, height, depth) of the box in woorld coords
	 * i.e. the 'right', 'top', 'back' corner of the box in world space */
 	public Vector3 boundary = new Vector3();

	// cache int values of the origin, probably pre-mature optimization
	public int x, y, z;

	public CubeDataGrid() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (int z = 0; z < width; z++) {
					grid[y][x][z] = new CubeData();
				}
			}
		}
	}

	public static CubeDataGrid create(float originX, float originY, float originZ) {
		//CubeDataGrid cdg = Pools.obtain(CubeDataGrid.class);
		CubeDataGrid cdg = Main.OBJECT$.getMainCDGPool().obtain();
		cdg.init(originX, originY, originZ);
		return cdg;
	}

	public void init(float originX, float originY, float originZ) {
		this.origin.set(originX, originY, originZ);
		this.center.set(origin).add(width / 2, height / 2, depth / 2);
		this.boundary.set(origin).add(width, height, depth);
		this.x = MathUtils.round(origin.x);
		this.y = MathUtils.round(origin.y);
		this.z = MathUtils.round(origin.z);
		numElements = width * height * depth;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (int z = 0; z < depth; z++) {
					//CubeData cubeData = Pools.obtain(CubeData.class);
					CubeData cubeData = grid[y][x][z];
					//cubeData.getPosition().set(x + origin.x, y + origin.y, z + origin.z);
					cubeData.setX((short) (x + origin.x));
					cubeData.setY((short) (y + origin.y));
					cubeData.setZ((short) (z + origin.z));
				}
			}
		}
	}

	public boolean hasCubeAt(Vector3 vec) {
		return hasCubeAt(vec.x, vec.y, vec.z);
	}


	public boolean hasCubeAt(float worldX, float worldY, float worldZ) {
		return (worldX >= origin.x && worldX < boundary.x &&
				worldY >= origin.y && worldY < boundary.y &&
				worldZ >= origin.z && worldZ < boundary.z);

		/*int y = MathUtils.floor(worldY - origin.y);
		int x = MathUtils.floor(worldX - origin.x);
		int z = MathUtils.floor(worldZ - origin.z);
		boolean result = y >= 0 && y < height && x >= 0 && x < width && z >= 0 && z < depth;
		//System.out.printf("check for cube at, xyz: %d, %d, %d -- %s\n", x, y, z, result);
		return result;*/
	}

	public CubeData getCubeAt(Vector3 vec) {
		return getCubeAt(vec.x, vec.y, vec.z);
	}

	public CubeData getCubeAt(float worldX, float worldY, float worldZ) {
		int y = MathUtils.floor(worldY - origin.y);
		int x = MathUtils.floor(worldX - origin.x);
		int z = MathUtils.floor(worldZ - origin.z);
		return grid[y][x][z];
	}

	public int numberOfHiddenFaces() {
		int total = 0;
		for (CubeData cubeData : this) {
			total += cubeData.getHiddenFacesCount();
		}
		return total;
	}

	public int getChunkLocalElevation(float xf, float zf) {
		int x = MathUtils.floor(xf);
		int z = MathUtils.floor(zf);
		int y;
		for (y = height-1; y >= 0; y--) {
			CubeData cube = getCubeAt(x, y, z);
			if (cube.getCubeType() != CubeTypes.Void) {
				return y;
			}
		}
		return -1;
	}

	// ITERATOR SECTION -------------------------------

	private CubeDataGridIterator iterator = null;

	@NotNull
	@Override
	public Iterator<CubeData> iterator() {
		/*if (iterator == null) {
			iterator = new CubeDataGridIterator(this);
		}
		iterator.reset();*/
		if (iterator == null) {
			iterator = new CubeDataGridIterator();
			iterator.setParentCDG(this);
		}
		iterator.reset();
		return iterator;
	}

	private class CubeDataGridIterator implements Iterator<CubeData> {
		int y;
		int x;
		int z;
		int maxY;
		int maxX;
		int maxZ;

		public void reset() {
			y = 0;
			x = 0;
			z = 0;
		}


		public CubeDataGridIterator() {

		}

		public void setParentCDG(CubeDataGrid cdg) {
			maxY = height - 1;
			maxX = width - 1;
			maxZ = depth - 1;
		}

		@Override
		public boolean hasNext() {
			return y <= maxY && x <= maxX && z <= maxZ;
		}

		@Override
		public CubeData next() {
			CubeData cubeData = grid[y][x][z];
			// increment index
			if (z >= maxZ) {
				if (x >= maxX) {
					// this will go past array bounds, which call hasNext to return false
					y++;
					x = 0;
					z = 0;
				} else {
					x++;
					z = 0;
				}
			} else {
				z++;
			}
			return cubeData;
		}

		@Override
		public void remove() {

		}
	}

	public int numberOfNonVoidCubes() {
		int total = 0;
		for (CubeData cube : this) {
			if (cube.getCubeType() != CubeTypes.Void) {
				total++;
			}
		}
		return total;
	}

	public void free() {
		/*for (CubeData cube : this) {
			Pools.free(cube);
		}*/
		//Pools.free(this);
		Main.OBJECT$.getMainCDGPool().free(this);
	}

	@Override
	public int hashCode() {
		return org.jrenner.learngl.utils.UtilsPackage.threeIntegerHashCode(x, y, z);
	}

	@Override
	public String toString() {
		return String.format("%.2f, %.2f, %.2f", origin.x, origin.y, origin.z);
	}
}
