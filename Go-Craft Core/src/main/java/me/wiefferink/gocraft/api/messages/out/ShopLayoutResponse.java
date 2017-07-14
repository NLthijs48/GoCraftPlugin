package me.wiefferink.gocraft.api.messages.out;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import me.wiefferink.gocraft.Log;

import java.util.List;

public class ShopLayoutResponse extends Response {

	public ShopLayout shopLayout;

	public ShopLayoutResponse() {
		super("shopLayout/UPDATE");

		try {
			HttpResponse<ShopLayout> shopLayoutResponse = Unirest.get("https://www.minecraftmarket.com/api/1.5/c9d4d60b31d404df124b1021c5731dce/gui")
					.header("Accept", "application/json")
					.asObject(ShopLayout.class);
			if(shopLayoutResponse.getStatus() != 200) {
				Log.error("Requesting MinecraftMarket data failed:", shopLayoutResponse.getStatus(), shopLayoutResponse.getStatusText(), shopLayoutResponse.getBody());
				return;
			}

			shopLayout = shopLayoutResponse.getBody();
		} catch(UnirestException e) {
			Log.error("Failed to get MinecraftMarket data:", e);
		}
	}

	public class ShopLayout {
		public List<ShopCategory> categories;
		public List<ShopItem> result;
		public String status;
	}

	public class ShopCategory {
		public long id;
		public String name;
		public long order;
		public String iconid;
	}

	public class ShopItem {
		public long id;
		public String name;
		public long categoryid;
		public String category;
		public String description;
		public String price;
		public String currency;
		public String iconid;
		public String url;
		public List<String> required;
	}
}
