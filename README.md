**Team AMA**
**Ciulinca Andra Stefania - 324CA**
**Similea Alin Andrei - 324CA**
**Stefan Miruna Andreea -324CA**

# Halite Project


### First Stage

We have created a static method decideMove in the Direction class, which is 
responsible for choosing the most effective way in which we can move from the 
current position. 

Firstly, we check if our current strength is less than the site production * 5
(we chose 5 as suggested in the game documentation). In this case, we remain on
the same site until we reach a higher strength. If the strength is high enough,
we need to find the best direction to conquer. For this, we created 2 
arrayLists: "directionsOk" contains the neighbouring cardinals (North, south, 
east, west) that do not already belong to us and that we can conquer; and 
"directionsOccupied" contains the cardinals that are already ours.

For each of the 4 possible cardinals, we check if its corresponding site has 
our ID and if it in not ours, we also check if we can conquer it by comparing 
its strength to the current strength.

If all the possible suroundings belong to us (all cardinals are occupied by
us), we will try to move north until we reach the end of the coloumn. When we 
are finished conquering the whole coloumn, we move horizontally (west).

In order to identify the most effective direction, we need to sort the 
available directions (the elements of the "directionsOk" arraylist) by 
production in descending order. In this way, the direction with the highest 
production will be situated on the first position of the arraylist and we need 
to return it. If the arraylist is empty, it means that there are no available 
directions to move, so we should stay still.


### Second Stage

We have modified the "decideMove" method. First of all, we create (with the
help of the "getEnemyPositions" function) an arraylist containing the locations
of the enemies. If the list is empty, meaning that there are no enemies in the
grid, we just want to occupy the entire map, like in the first round. This is
exactly what the "getDirectionNoEnemies" method does. We call the
"increaseProduction" function, which is very similar to the implementation of
the "decideMove" function from the first round and returns the best direction
in which we can move.

If there is at least one enemy on the map, the strategy is different. We need 
to store the number of sites that belong to us. This will help us identify the 
most suitable strategy depending on how much territory we own on the map at the
current moment. If it is less than 25, we are at the beginning of the game and 
we need to expand circularly so as to get as much territory as we can. In the 
next stage (between 26 and 100), we need to concentrate on becoming more 
powerful (acquiring strength) rather than just expanding. In the third stage 
(>100), we are actually fighting with enemies. We try to identify the closest 
enemy and if it is closer than 1 site to us and we are not involved in another 
attack at that moment, we get into a fight with the nereast enemy. Our strategy
is to combine our strength with the neighbours so as to obtain the biggest 
power. If the enemy is sitatuated at a distance between 1 and 2 from our 
position, we need to prepare attack because we are likely to get into a fight 
soon. 

If the enemy is situated at a bigger distance than 2 (but smaller than 7) from 
us, we need to store our neighbours (also belonging to us) in order to see how 
much support we have. If we have at least 2 neighbours belonging to us, we move
towards the enemy.

If we are currently involved in an attack, we have 2 options: we either keep 
our circular movement, or move towards enemy if we have the necessary power to 
conquer.  

If there are no enemies near us, we apply the same strategy as when the map is 
empty.


### Place
We placed 52nd out of over 110 teams.