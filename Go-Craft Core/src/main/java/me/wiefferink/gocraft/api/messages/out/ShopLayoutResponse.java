package me.wiefferink.gocraft.api.messages.out;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import me.wiefferink.gocraft.Log;

import java.util.List;

public class ShopLayoutResponse extends Response {

	public ShopCategories categories;
	public ShopItems items;

	public ShopLayoutResponse() {
		super("shopLayout/UPDATE");

		try {
			HttpResponse<ShopCategories> categoriesResponse = Unirest.get("https://minecraftmarket.com/api/v1/plugin/9d51d8c18f7b7fd72144eeed48c93fdc/categories/?format=json")
					.header("Accept", "application/json")
					.asObject(ShopCategories.class);
			if(categoriesResponse.getStatus() != 200) {
				Log.error("Requesting MinecraftMarket categories data failed:", categoriesResponse.getStatus(), categoriesResponse.getStatusText(), categoriesResponse.getBody());
				return;
			}

			categories = categoriesResponse.getBody();
		} catch(UnirestException e) {
			Log.error("Failed to get MinecraftMarket categories data:", e);
		}

		try {
			HttpResponse<ShopItems> itemsResponse = Unirest.get("https://minecraftmarket.com/api/v1/plugin/9d51d8c18f7b7fd72144eeed48c93fdc/items/?format=json")
					.header("Accept", "application/json")
					.asObject(ShopItems.class);
			if(itemsResponse.getStatus() != 200) {
				Log.error("Requesting MinecraftMarket items data failed:", itemsResponse.getStatus(), itemsResponse.getStatusText(), itemsResponse.getBody());
				return;
			}

			items = itemsResponse.getBody();
		} catch(UnirestException e) {
			Log.error("Failed to get MinecraftMarket items data:", e);
		}
	}

	public class ShopCategories {
		public List<ShopCategory> results;
		public long count;

	}

	public class ShopCategory {
		public long id;
		public String name;
		public String gui_description;
		public String gui_icon;
		public List<String> subcategories;
		public List<ShopItem> items;
		public long order;
	}

	public class ShopItems {
		public List<ShopItem> results;
		public long count;
	}

	public class ShopItem {
		public long id;
		public long order;
		public String name;
		public String gui_description;
		public String gui_icon;
		public String price;
		public String gui_url;
		public List<String> required;
	}
}
