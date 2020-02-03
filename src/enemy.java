//this is to store the abstract data for all enemy types, we are expected to create specific enemies tha implements this
abstract class enemy{
    //we need a health value, hitbox size, movement speed for all enemies
    int health;
    int hitboxSize;
    int speed;//speed determents how fast a eney moves when they move. For immobile enemies, speed = 0
    int[] icon;
}