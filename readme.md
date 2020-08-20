Steam Grid Kit
==================================================

What is it for
--------------------------------------

Steam Grid Kit is an app that helps you to manage your Steam shortcuts. It has 3 separate purposes:

1. Preventing your shortcuts from being deleted from Steam. The bug that deletes your shortcuts is still present in 2020.
2. Automatically adds the games from your games folder to Steam, finds images for them.
3. Loading images from Steam and SteamGridDb for problematic games. (Like games that aren't present on Steam or emulated games for old consoles)


Backing up your shortcuts
--------------------------------------

1. Set the path to shortcuts.vdf file
2. Press start


Adding your games to Steam
--------------------------------------
1. Set the path to shortcuts.vdf file
2. Set the path to your games directory
3. Press Start

Tasks
----------------
**Find Existing Shortcuts**

Loads data from Steam shortcuts.vdf file into the app and saves it and it's data into Steam Grid Kit folder. This is useful for backing up existing shortcuts.vdf file 
as there's still a bug that can delete it from the system (usually it happens if you forcefully restart the PC whel Steam is shutting down). In cached mode it uses the shortcuts.vdf
file from Steam Grid Kit folder.

**Find Existing Images**

Loads the images from the Steam folder into Steam Grid Kit folder. This task is crucial to be executed if you've already set custom images for your shortcuts. *Note that the loaded images will be treated as user-defined and
won't be overriden with automatic image search.*

**Find Game Folders**

Finds game folders in the games' folder. This command simply lists the games directories and saves them in the app. 
At this stage you can check the list of game folders that will be processed and add some folders to the ignored folders list.

**Load Steam Games**

Loads / updates all games from Steam API to the app. Your game directories will be matched against this list.  Skipped in the cached data mode.  

**Find Executables**

Searches for executable files in the game folders and lists them accordingly to similarity to the game title. 
This is the longest task since it goes recursively over all the game folders. Games with found executables skipped in the cached data mode.
 
 **Find Steam IDs**
 
 Searches steam game IDs for the listed games accordingly to the folder name. Steam IDS are being updated with **Load Steam Games** task.
 Folders with found steam IDS skipped in the cached data mode.
 
 **Load Steam Images**
 Loads images for the listed games accordingly to the selected Steam ID. It loads 4 images: cover for the grid mode, 
 cover for the big picture mode, background picture for game page,  logo for the background picture. 
 
 
 **Transfer to Steam**
 
Creates shortcuts.vdf file and copies it to the steam folder. Before copying, it backups the original file into the backup folder. 
After copying the vdf file it also copies images to grid subfolder in the steam folder.