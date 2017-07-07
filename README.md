# BL4DEUtil
## Commands:
/Blade - Prints info message regarding the plugin<br />
/Blade help - Prints commands and their respective usages<br />
/Blade debug - Toggles debug information  
/GMC - Sets users gamemode to creative<br />
/GMS - Sets users gamemode to survival<br />
/Ranks [labrat|technician|scientist]<br />
/LastOnline - Shows the last 10 players who have logged onto the server with their last login times  
/GetChunkLoadingWard - Information for getting a extrautils2:chunkloader  
/GetChunkLoadingWard Confirm - Takes 3 emeralds and gives the player one chunk loaded  

## Permissions:
`bl4de.base` - /Blade, /Blade help<br />
`bl4de.debug.base` - /Blade debug  
`bl4de.gamemode.creative` - /GMC<br />
`bl4de.gamemode.survival` - /GMS<br />
`bl4de.ranks.base` - /Ranks, /Ranks [labrat|technician|scientist]<br />
`bl4de.getclw.base` - /GetChunkLoadingWard, /GetCLW  
`bl4de.getclw.confirm` - /GetChunkLoadingWard Confirm, /GetCLW C  
`bl4de.lastonline.base` - /LastOnline, /LO  

## TODO:
* Make commands clickable in /GetCLW and /Blade ?
* Keep track of how many chunk loading wards each player has recieved and enable a limit
* Allow to see the time a specific player was online (/LastOnline TheCahyag)
* Add /GetCLW and /LastOnline to the /Blade ? commands
* Add functionality to the CLW check so if the player breaks a chunk loading ward that is supporting a AE2Stuff block the action is canceled, or all the AE2Stuff blocks are destroyed and placed in the inventory of the player
* Add functionality to the /LO command and display near the time how long since the player has been on relative to the current time. (1 day ago, 3 hours ago, ... etc)