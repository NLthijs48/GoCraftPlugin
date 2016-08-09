package me.wiefferink.gocraft.shop.features;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;

public class MapKitsFeature extends ShopFeature {

	private String map;

	public MapKitsFeature(Kit kit) {
		super(kit);
		map = kit.getDetails().getString("map");
	}

	@Override
	public boolean allowsBuy(ShopSession session) {
		return map == null || map.equals(getCurrentMap());
	}

	@Override
	public void indicateRestrictedBuy(ShopSession session) {
		GoCraft.getInstance().message(session.getPlayer(), "shop-notInMap", getMapName(), kit.getName());
	}

	@Override
	public String getBuyStatusLine(ShopSession session) {
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
	 * @return A mapname if restricted to a map, otherwise null
	 */
	public String getMap() {
		return map;
	}

	/**
	 * Get the name of the map as defined by MapSwitcher
	 * @return The name of the map
	 */
	public String getMapName() {
		String result = GoCraft.getInstance().getMapSwitcherLink().get().getMapName(getMap());
		if(result == null) {
			result = "Upcoming";
		}
		return result;
	}

	/**
	 * Get the map that is currently active
	 * @return The map that is currently active
	 */
	public String getCurrentMap() {
		return GoCraft.getInstance().getMapSwitcherLink().get().getCurrentMapIdentifier();
	}
}
