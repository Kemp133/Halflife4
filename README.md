# Team Project - By Team HalfLife
## Making a 2D top down game in Java
### (Using JavaFX as a graphics library)


---
##For the programming IDE of the Gods (i.e. IntelliJ)
When creating an instance of the project, you'll need to add the following text to the VM Options:

<code>
--module-path [PATH_TO_FX] --add-modules=javafx.controls,javafx.fxml
</code>

Where [PATH_TO_FX] is the file path to wherever javafx's 'lib' file is on your system (e.g. "C:\Program Files\javafx\lib")

If you're lazy (like me), you can create a global path variable, which when you put ${PATH_TO_FX}, puts the path in for 
you. To do this:

1) Press Ctrl-Alt-S
2) On the left menu, go to Appearance & Behaviour, then click on Path Variables
3) In the menu on the right, press the '+' button
4) In the menu that pops up, enter a name for the variable (e.g. PATH_TO_FX) and then enter (or browse) to the lib 
folder inside of your javafx install location (the path should end in "\lib") and then click okay
5) Click Apply
6) Go back to the run configurations, and where it says --module-path, put {name of path variable} 
(e.g. --module-path ${PATH_TO_FX})
7) Profit (...and your code should build too. Then, wherever you need to put this command again 
(in the event you have more than one build configuration that is), you can just put ${PATH_TO_FX} instead)

If you do it this way, your VM Options should look something like this:

<code>
--module-path ${PATH_TO_FX} --add-modules=javafx.controls,javafx.fxml
</code> 