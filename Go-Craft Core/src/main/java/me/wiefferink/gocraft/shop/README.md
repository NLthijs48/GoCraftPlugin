# Shop
Implements the `/shop` command.

* Can show categories, kit contents and items, with a menu bar to switch between categories.
* Shop concepts:
    * `ShopSession` is created whena a player opens the shop, removed when it is closed.
    * `Category` a list of `Kit` instances
    * `Kit` a list of items that can be bought.
* Code structure concepts:
    * `Shop` glue to receive the command and player interactions and pass them to the `ShopSession`.
    * `View` complete inventory (except the bottom row) that shows some information (category contents, kit contents, item details).
    * `Button` one item in the inventory that can be clicking to perform an action (switch to other view, buy kit, close shop, etc).
    * `ShopFeature` implementing one part of the kit shop:
        * Show a status line when hovering a kit.
        * Can restrict buying the kit.
    * `Sign` sign that allows you to perform an action (open shop, buy kit)    
