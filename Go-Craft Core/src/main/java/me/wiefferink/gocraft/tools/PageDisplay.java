package me.wiefferink.gocraft.tools;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class PageDisplay extends Feature {
	public CommandSender target;
	private int maxItems = 20;
	private int itemsPerPage = maxItems-2;
	private int itemCount;
	private String baseCommand;
	private List<Message> rendered;

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
	public abstract Message renderHeader();

	/**
	 * Render a message that there are no items in the list
	 */
	public abstract Message renderEmpty();

	/**
	 * Render an item (supposed to be on one line)
	 * @param itemNumber The item number to render (0 is the first of the list, will never be itemCount or higher)
	 */
	public Message renderItem(int itemNumber) {
		return null;
	}

	/**
	 * Render all items, if it returns true calling renderItem() for each item is skipped
	 * @param start The starting item
	 * @param end The last item
	 * @return true if the work is complete, false to let renderItem() handle it
	 */
	public boolean renderItems(int start, int end) {
		return false;
	}

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
	public PageDisplay renderPage(String pageInput) {
		rendered = new ArrayList<>();
		int page = 1;
		if(pageInput != null && Utils.isNumeric(pageInput)) {
			try {
				page = Integer.parseInt(pageInput);
			} catch(NumberFormatException e) {
				message(Message.fromKey("page-wrong").replacements(pageInput));
				return this;
			}
		}
		if(itemCount <= 0) {
			message(renderEmpty());
			return this;
		}

		long startTime = Calendar.getInstance().getTimeInMillis();
		message(renderHeader());
		// Page entries
		int totalPages = (int)Math.ceil(itemCount/(double)itemsPerPage); // Clip page to correct boundaries, not much need to tell the user
		int renderItemsPerPage = itemsPerPage;
		if(itemCount == itemsPerPage+1) { // 19 total items is mapped to 1 page of 19
			renderItemsPerPage++;
			totalPages = 1;
		}

		page = Math.max(1, Math.min(totalPages, page));
		int startItem = (page-1)*renderItemsPerPage;
		int endItem = Math.min(page*renderItemsPerPage, itemCount)-1;
		if(!renderItems(startItem, endItem)) {
			for(int i = startItem; i <= endItem; i++) {
				message(renderItem(i));
			}
		}

		// Page status (no need for a footer if there is only one page)
		if(totalPages > 1) {
			Message footer = Message.empty();
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
			for(int i = rendered.size(); i < maxItems-1; i++) {
				message(Message.fromString(" "));
			}
			message(footer);
		}
		long endTime = Calendar.getInstance().getTimeInMillis();
		//GoCraft.debug("Rendering page for command '"+baseCommand+"' took", endTime-startTime, "ms");
		return this;
	}

	/**
	 * Show the rendered page
	 */
	public void show() {
		for(Message message : rendered) {
			message.send(target);
		}
	}

	/**
	 * Adds a message to the output
	 * @param message The message to add to the output
	 */
	public void message(Message message) {
		if(message != null) {
			rendered.add(message);
		}
	}
}