# Laserdome
A duel between the two teams with the most points
All concrete in the arena will be replaced by the color of the teams participating. Each side for each team. 
The carpets in the actual playing field will do the same





## Commands
**/startLaserdome** 
- starts the game 

**/setLaserdomeLobby** 
- sets lobby location that players are teleported to after game ends

**/setLaserdomeTeamSpawn [A or B]** 
- set spawn spot for a participating team, team A is one side, team B is the other side

**setLaserdomeSpecSpawn** 
- sets the spawn point in the stands, it's where everyone gets teleported too at the start

**setLaserdomePos1**
- sets corner 1 of the whole area

**setLaserdomePos2**
- sets corner 2 of the whole area. This should be diagonally opposite of pos1

**setLaserdomeArenaLoc1**
- sets corner 1 of the playing field 

**setLaserdomeArenaLoc2**
- sets corner 2 of the playing field


## Version Log:
- 1.0: Base Working Laserdome game
- 1.1: Arrows are time-limited so teams cannot hold them indefinitely

## Dependencies:
- Teams Plugin (https://github.com/cardsandhuskers/TeamsPlugin)
    - note: this must be manually set up as a local library on your machine to build this plugin
- optional: PlaceholderAPI for scoreboard placeholders