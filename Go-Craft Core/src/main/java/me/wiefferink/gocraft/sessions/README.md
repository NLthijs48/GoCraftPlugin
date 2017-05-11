# Sessions
Contains code that implements a player activity tracker.

* `SessionTracker` is used in BungeeCord to track player.
* `BungeeSession` is a visit of a player, containing of zero or more `ServerSession` entries.
* `ServerSession` is a visit of a player to a specific server.
* The session classes are used in BungeeCord and Spigot, and therefore contain only Java imports.
* Hibernate is used to manage these sessions in the database.