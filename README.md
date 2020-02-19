# Team Project - By Team HalfLife
## Making a 2D top down game in Java
### (Using JavaFX as a graphics library)


---
## For the programming IDE of the Gods (i.e. IntelliJ)
When creating a run profile for the project, you'll need to add the following text to the VM Options:

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

---

##Reloading library problem
When loading the project, there seems to be a weird bug that means you have to navigate to "Project Structure", and then
in the Libraries section, delete and relink the library files. If this happens to you again, follow the below steps:

1) Delete and reload the affected libraries (e.g. Go to Project Structure [Ctrl-Shift-Alt-S] (not the best shortcut, I know) and then to Libraries)
2) When you do this, IntelliJ will show you that you've changed the halflife.iml file, even though it's in the .gitignore file. Do not commit this file
3) Instead, right click on the halflife.iml file and click the "Show In Explorer" option from the menu
4) When the explorer window opens up, open a git bash terminal and type the following: git rm halflife.iml --cached
5) When you go back into IntelliJ again, the file should either show up with orange/yellow or red font
6) When you next commit, make sure that this file isn't in the files you're commiting, if it is then repeat steps 3-5
7) You should be able to commit and push your code without halflife.iml showing up, nor should it break again when someone else syncs/pulls the code
8) Profit?