# Features
Contains (mostly simple) features that can be turned on/off using the `config.yml` file. They all extend the `Feature` class, which provides commonly used functionality to these classes (and also to a lot of other classes in this plugin).

Features need to declare their config options in `config.yml` and need to be added to the `GoCraft` class to instantiate them, otherwise they are completely in 1 file.