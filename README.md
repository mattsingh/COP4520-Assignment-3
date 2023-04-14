# Assignment 3

## Problem 1

### Strategy

In Problem 1 the strategy I used was for a servant to take from an unordered bag and place into the ordered chain and then subsequently remove a present from the front of the chain and write a thank you card. The servant would then repeat this process until the bag was empty. While doing all this the servant would be able to check if the ordered chain had contained a present with a certain tag previously. This was simulated to happen by chance 20% of the time.
We used a Lazy List from "The Art of Multiprocessing" as our data structure for the chain of presents. This list was to be sorted by using the present's tag as a key. I also modified the `remove` method to be a `poll` method which removes and returns just the first item of the list.

### How to run

```bash
cd "Problem 1"
javac TheBirthDayPresentsParty.java
java TheBirthDayPresentsParty
```

## Problem 2

### How to run

```bash
cd "Problem 2"
javac AtmosphericTemperatureReadingModule.java
java AtmosphericTemperatureReadingModule
```

