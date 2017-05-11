# Information
Contains the implementation of the `/information [player]` command.
 
* `InformationManager` receives the command, creating an `InformationRequest` and starts the execution.
* `InformationProvider` is the abstract class to represent a thing that has information available for the command.
    * Most providers have some conditions before they show the information, depending on permissions, rank or config files.
* `InformationRequest` performs the following actions:
    * Executes the `showSync()` method of all `InformationProvider` classes.
    * Switches to an async thread and executes the `showAsync()` methods.
    * Switches to a sync thread and shows all collected messages to the `CommandSender` requesting the information.
    
  