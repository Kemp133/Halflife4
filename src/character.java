//this is to store the abstract data for all characters
abstract class Character{
    //we need a health value, hitbox size, movement speed for all characters
    //look up  what is point2D
    int health;
    int hitboxSize;
    int speed;//speed determents how fast a eney moves when they move. For immobile characters, speed = 0
    int[] icon;
}