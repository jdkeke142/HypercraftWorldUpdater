# WorldUpdater
The purpose of this simple plugin is to help you keep your world up to date when switching from one version to another, how? This plugin will simply remove all chunks that do not touch WorldGuard regions, so you will need to make a region for each of your players' bases, and run the "convertmyworld" command.

You need to perform the command in Spigot 1.13, and then you can put this world on Spigot 1.14 server, avoid loading chunks in the meantime.

The chunks deletion can be long, and it could bring your server to time out, to avoid this, put the "timeout-time" key in spigot.yml on a higher value, like 1000.

This plugin require WorldGuard and FastAsyncWorldEdit to work properly.

Attention! Please make a backup of your world before using this command, I will not be responsible for any damage caused on your world if you do not do this!

