package me.wiefferink.gocraft.rewards;

import me.wiefferink.gocraft.sessions.GCPlayer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(indexes = {
		@Index(columnList = "completed", name = "completed"),
		@Index(columnList = "server", name = "server"),
})
public class Reward {

	public enum RewardSource {
		VOTING
	}

	public enum RewardType {
		MONEY,
		COMMAND
	}

	@Id
	@GeneratedValue
	@Column
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private GCPlayer gcPlayer;

	@Column(nullable = false)
	private Date at;

	@Column(nullable = false)
	private boolean completed;

	@Column(nullable = false)
	private String server;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RewardSource source;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RewardType type;

	@Column
	private String message;

	@Column
	private int requiredSlots;

	////////// Type-specific columns
	// What length should it be? With @Lob it should be any length
	@Lob
	@Column
	private String command;

	@Column
	private double money;

	protected Reward() {}

	/**
	 * Basic constructor to fill in the required fields
	 * @param gcPlayer Player that should receive the rewards
	 * @param source Source of the reward
	 */
	public Reward(GCPlayer gcPlayer, RewardSource source, String server) {
		this.gcPlayer = gcPlayer;
		this.source = source;
		this.server = server;
		this.at = new Date();
	}

	////////// Builder methods

	/**
	 * Add a message to send when giving the reward
	 * @param message Message to send
	 */
	public void message(String message) {
		this.message = message;
	}

	/**
	 * Set the money that the player should get
	 * @param money Amount of money
	 */
	public void money(double money) {
		this.money = money;
		this.type = RewardType.MONEY;
	}

	/**
	 * Add a command to execute
	 * @param command Command to give a reward
	 */
	public void command(String command) {
		this.command = command;
		this.type = RewardType.COMMAND;
	}

	/**
	 * Set the number of inventory slots that is required to receive this reward
	 * @param slots Number of required empty slots
	 */
	public void requiredSlots(int slots) {
		this.requiredSlots = slots;
	}

	////////// Getters

	/**
	 * Get the player that did the vote
	 * @return The GCPlayer that voted
	 */
	public GCPlayer getPlayer() {
		return gcPlayer;
	}

	/**
	 * Get the vote date
	 * @return The date at which has been voted
	 */
	public Date getAt() {
		return at;
	}

	/**
	 * Return the amount of money to be given
	 * @return Money to be given if getType()==RewardType.MONEY, otherwise 0
	 */
	public double getMoney() {
		return money;
	}

	/**
	 * Return the command to be executed
	 * @return Command to be executed if getType()==RewardType.COMMAND, otherwise null
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Get the source of this reward
	 * @return RewardSource indicating why the player received this reward
	 */
	public RewardSource getSource() {
		return source;
	}

	/**
	 * Get the reward type
	 * @return RewardType indicating what should be done when giving this reward
	 */
	public RewardType getType() {
		return type;
	}

	/**
	 * Check if the reward is given to the player
	 * @return true if already given to the player, otherwise false
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * Mark as completed
	 */
	public void complete() {
		completed = true;
	}

	/**
	 * Get the message that should be send when giving the reward
	 * @return Message that should be send, or null if none
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Get the server this reward is for
	 * @return Id of the server this reward should be given on
	 */
	public String getServer() {
		return server;
	}

	/**
	 * Get the required number of empty inventory slots
	 * @return Required number of empty inventory slots
	 */
	public int getRequiredSlots() {
		return requiredSlots;
	}

	@Override
	public String toString() {
		return "Reward(player="+gcPlayer.getName()+", at="+at+")";
	}
}
