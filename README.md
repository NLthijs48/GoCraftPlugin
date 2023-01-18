# Go-Craft
Spigot and BungeeCord plugin for the Go-Craft server, which currently does not exist anymore.

Published because some code might still be useful, and I generally like to keep code alive if possible.
Most likely this does not compile correctly anymore, some Maven repositories and such are probably gone, sorry!

## Building
* Clone this repository
* Use `maven install`
* Optionally add these options to the Maven command:
    * **Source:** generate a jar file that has the sources of the project, add `-Dsources`
    * **Javadoc:** generate javadocs to the given directory, add `-Djavadocs=path/to/target/folder`
    * **Copy jar file:** copy the resulting jar file to the given location (automatic installation on your test server), add `-DcopyResult=path/to/plugins/folder`

## Structure
* This plugin is a multi-module Maven project:
    * The `Go-Craft Core` module builds the final plugin and contains all version-independent code.
    * The `Interfaces` module is used as dependency in the `Go-Craft Core` module and all version specific modules and contains interfaces for them to integrate.
    * All modules named after Bukkit versions have version specific code, of which one is loaded on startup by the Spigot plugin.
* Packages in the `Go-Craft Core` module contain `README.md` files explaining what the package contains.
* The `Go-Craft Core` module compile 1 `.jar` file containing the Spigot and BungeeCord plugin sources, Spigot will find the `plugin.yml` file and load the `GoCraft` class, BungeeCord will find the `bungee.yml` file and load the `GoCraftBungee` class.
