package me.wiefferink.gocraft.shop.features;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;

public class MapKitsFeature extends Feature {

	private String map;

	public MapKitsFeature(Kit kit) {
		super(kit);
		map = kit.getDetails().getString("map");
	}

	@Override
	public boolean allows(ShopSession session) {
		return map == null || map.equals(getCurrentMap());
	}

	@Override
	public void indicateRestricted(ShopSession session) {

	}

	@Override
	public String getStatusLine(ShopSession session) {
		if (map != null) {
			if (map.equals(getCurrentMap())) {
				return "&2Map: " + getMapName();
			} else {
				return "&4Map: " + getMapName();
			}
		}
		return null;
	}

	/**
	 * Get the map this kit is restricted to
	 *
	 * @return A mapname if restricted to a map, otherwise null
	 */
	public String getMap() {
		return map;
	}

	/**
	 * Get the name of the map as defined by MapSwitcher
	 *
	 * @return The name of the map
	 */
	public String getMapName() {
		return GoCraft.getInstance().getMapSwitcherLink().get().getMapName(getMap());
	}

	/**
	 * Get the map that is currently active
	 *
	 * @return The map that is currently active
	 */
	public String getCurrentMap() {
		return GoCraft.getInstance().getMapSwitcherLink().get().getCurrentMap();
	}
}
