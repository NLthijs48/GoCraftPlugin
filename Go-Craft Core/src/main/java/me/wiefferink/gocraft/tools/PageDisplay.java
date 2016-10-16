package me.wiefferink.gocraft.tools;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Calendar;

public abstract class PageDisplay extends Feature {
	public CommandSender target;
	private int maxItems = 20;
	private int itemsPerPage = maxItems-2;
	private int itemCount;
	private String baseCommand;

	/**
	 * Create a page display for a certain target
	 * @param target The CommandSender to display for
	 */
	public PageDisplay(CommandSender target, int itemCount, String baseCommand) {
		this.target = target;
		this.itemCount = itemCount;
		this.baseCommand = baseCommand;
	}

	/**
	 * Render the header of the list (supposed to be on one line)
	 */
	public abstract void renderHeader();

	/**
	 * Render a message that there are no items in the list
	 */
	public abstract void renderEmpty();

	/**
	 * Render an item (supposed to be on one line)
	 * @param itemNumber The item number to render (0 is the first of the list, will never be itemCount or higher)
	 */
	public abstract void renderItem(int itemNumber);

	/**
	 * Set the maximum height of the page display (default 20)
	 * @param maxItems The maximum height of the page display in lines
	 */
	public void setMaxItems(int maxItems) {
		this.maxItems = maxItems;
		this.itemsPerPage = maxItems-2;
	}

	/**
	 * Render a page
	 * @param pageInput The page number to render (or the first one if not given)
	 */
	public void renderPage(String pageInput) {
		int page = 1;
		if(pageInput != null && Utils.isNumeric(pageInput)) {
			try {
				page = Integer.parseInt(pageInput);
			} catch(NumberFormatException e) {
				plugin.message(target, "page-wrong", pageInput);
				return;
			}
		}
		if(itemCount <= 0) {
			renderEmpty();
			return;
		}

		long start = Calendar.getInstance().getTimeInMillis();
		renderHeader();
		// Page entries
		int totalPages = (int)Math.ceil(itemCount/(double)itemsPerPage); // Clip page to correct boundaries, not much need to tell the user
		int renderItemsPerPage = itemsPerPage;
		if(itemCount == itemsPerPage+1) { // 19 total items is mapped to 1 page of 19
			renderItemsPerPage++;
			totalPages = 1;
		}

		page = Math.max(1, Math.min(totalPages, page));
		int linesPrinted = 1; // header
		for(int i = (page-1)*renderItemsPerPage; i < page*renderItemsPerPage && i < itemCount; i++) {
			renderItem(i);
			linesPrinted++;
		}

		// Page status (no need for a footer if there is only one page)
		if(totalPages > 1) {
			Message footer = Message.none();
			// Previous button
			if(page > 1) {
				footer.append(Message.fromKey("page-previous").replacements(baseCommand+" "+(page-1)));
			} else {
				footer.append(Message.fromKey("page-noPrevious"));
			}
			String pageString = ""+page;
			for(int i = pageString.length(); i < (totalPages+"").length(); i++) {
				pageString = "0"+pageString;
			}
			footer.append(Message.fromKey("page-status").replacements(page, totalPages));
			if(page < totalPages) {
				footer.append(Message.fromKey("page-next").replacements(baseCommand+" "+(page+1)));
			} else {
				footer.append(Message.fromKey("page-noNext"));
			}
			// Fill up space if the page is not full (aligns header nicely)
			for(int i = linesPrinted; i < maxItems-1; i++) {
				target.sendMessage(" ");
			}
			footer.send(target);
		}
		long end = Calendar.getInstance().getTimeInMillis();
		GoCraft.debug("Printing page for command '"+baseCommand+"' took", end-start, "ms");
	}
}