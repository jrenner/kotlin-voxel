package org.jrenner.learngl.cube;

import com.badlogic.gdx.math.Vector3;
import org.jrenner.learngl.Main;
import org.jrenner.learngl.gameworld.Chunk;
import org.jrenner.learngl.gameworld.World;

/** a 3d array of the all chunks in the game */
public class WorldChunkData {
	public int width, height, depth;

	public WorldChunkData(int width, int height, int depth, World world) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		//this.data = new CubeDataGrid[height][width][depth];
		// CODE DISABLED, now done lazily
/*		for (int y = 0; y <= height/chunkSize(); y++) {
			for (int x = 0; x <= width/chunkSize(); x++) {
				for (int z = 0; z <= depth/chunkSize(); z++) {
					origin.set(x, y, z);
					origin.scl(chunkSize());
					System.out.println("make cdg at origin: " + origin);
					CubeDataGrid cdg = new CubeDataGrid(chunkSize(), origin);
					world.applyWorldData(cdg);
					cdg.dirty = true;
					data[y][x][z] = cdg;
				}
			}
		}*/
	}

	/** workaround for bug in kotlin compiler
	 * https://youtrack.jetbrains.com/issue/KT-6586
	 */
	private int chunkSize() {
		return Chunk.OBJECT$.getChunkSize();
	}

	private static Vector3 tmp = new Vector3();

	// TODO test this method
	public CubeDataGrid getCDGByWorldPos(float xf, float yf, float zf) {
		int sz = chunkSize();
		int x = (int) xf / sz;
		int y = (int) yf / sz;
		int z = (int) zf / sz;
		Vector3 origin = tmp.set(x, y, z);
		origin.scl(sz);
		//System.out.println("make cdg at origin: " + origin + ", size: " + sz);
		CubeDataGrid cdg = CubeDataGrid.create(origin.x, origin.y, origin.z);
		Main.OBJECT$.getMainWorld().applyWorldData(cdg, org.jrenner.learngl.LearnglPackage.getWorld());
		cdg.dirty = true;
		return cdg;
	}


	/*private WorldChunkDataIterator iterator;

	@NotNull
	@Override
	public Iterator<CubeDataGrid> iterator() {
		if (iterator == null) {
			iterator = new WorldChunkDataIterator(this);
		}
		iterator.reset();
		return iterator;
	}*/

	/*class WorldChunkDataIterator implements Iterator<CubeDataGrid> {
		private WorldChunkData wcd;
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


		public WorldChunkDataIterator(WorldChunkData wcd) {
			this.wcd = wcd;
			maxY = wcd.height - 1;
			maxX = wcd.width - 1;
			maxZ = wcd.depth - 1;
		}

		@Override
		public boolean hasNext() {
			return y <= maxY && x <= maxX && z <= maxZ;
		}

		@Override
		public CubeDataGrid next() {
			CubeDataGrid cdg = wcd.data[y][x][z];
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
			return cdg;
		}

		@Override
		public void remove() {

		}
	}*/
}
