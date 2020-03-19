# Team Project - By Team HalfLife
## Making a 2D top down game in Java
### (Using JavaFX as a graphics library)
#### Does anybody actually read this?

---

## For the programming IDE of the Gods (i.e. IntelliJ)
When creating a run profile for the project, you'll need to add the following text to the VM Options:

<code>--module-path [PATH_TO_FX] --add-modules=javafx.controls,javafx.fxml,javafx.media</code>

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

<code>--module-path ${PATH_TO_FX} --add-modules=javafx.controls,javafx.fxml,javafx.media</code> 

---

## Reloading library problem
When loading the project, someone has accidentally included their halflife.iml file in the upload and it's caused all others who have pulled the code
to have their libraries unlink and break the solution. The fix is as below:

1) Delete and reload the affected libraries (e.g. Go to Project Structure [Ctrl-Shift-Alt-S] (not the best shortcut, I know) and then to Libraries)
2) When you do this, IntelliJ will show you that you've changed the halflife.iml file, even though it's in the .gitignore file. Do not commit this file
3) Instead, right click on the halflife.iml file and click the "Show In Explorer" option from the menu
4) When the explorer window opens up, open a git bash terminal and type the following: <code>git rm halflife.iml --cached</code>
5) When you go back into IntelliJ again, the file should either show up with orange/yellow or red font
6) When you next commit, make sure that this file isn't in the files you're commiting, if it is then repeat steps 3-5
7) You should be able to commit and push your code without halflife.iml showing up, nor should it break again when someone else syncs/pulls the code
8) Profit?

---

## Fixing Git
For some reason, the BasicBall class seems to have tried to kill our Git repo. While fixing Tom's git, this seems to have worked for him.

## <ins>**NOTE: BACKUP YOUR FILES WHICH YOU'VE MADE CHANGES IN, OTHERWISE THIS WILL GET RID OF ALL OF THEM!!!!**</ins>
<a href="https://stackoverflow.com/questions/1125968/how-do-i-force-git-pull-to-overwrite-local-files">Possible fix for Git problem, check if it applies to you
before you use it</a>

### How to check if the fix applies to you
1) Navigate to where the Halflife solution is for you (for me it's C:/Users/Lenovo/IdeaProjects/halflife)
2) In this file, run <code>git status</code>
3) If you see the following lines in your terminal, the fix probably applies to you:
    ```
    Changes not staged for commit:
        (use "git add \<file>..." fto update what will be committed)
        (use "git restore \<file>..." to discard changes in working directory)
                modified:   src/com/halflife3/Model/Ball/BasicBall.java
    ```
4) If you see this, then the fix applies to you. **REMEMBER TO BACK UP YOUR FILES BEFORE YOU DO IT**
5) If you don't see this, then it's something else. **DO NOT ATTEMPT THIS FIX**