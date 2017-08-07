# Management
Server management tools:
* `DistributionManager` does the following:
    * Implements `/update`
    * Updates plugins on the server on shutdown (version aware)
    * Updates config files if specified in `../GENERAL`
    * Updates the permission file with per rank permissions