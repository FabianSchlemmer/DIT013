
import java.util.SplittableRandom;
import java.util.Scanner;

import static java.lang.System.*;

/*
 *  The Mexico dice game
 *  See https://en.wikipedia.org/wiki/Mexico_(game)
 *
 */
public class Mexico {

	public static void main(String[] args) {
		new Mexico().program();
	}

	final SplittableRandom rand = new SplittableRandom();
	final Scanner sc = new Scanner(in);
	final int maxRolls = 3; // No player may exceed this
	final int startAmount = 3; // Money for a player. Select any
	final int mexico = 1000; // A value greater than any other

	void program() {
		// test(); // <----------------- UNCOMMENT to test

		int pot = 0; // What the winner will get
		Player[] players; // The players (array of Player objects)
		Player current; // Current player for round
		Player leader; // Player starting the round

		players = getPlayers();
		current = getRandomPlayer(players);
		leader = current;

		out.println("Mexico Game Started");
		statusMsg(players);

		while (players.length > 1) { // Game over when only one player left

			// ----- In ----------
			String cmd = getPlayerChoice(current);
			if ("r".equals(cmd)) {
				// --- Process ------
				// leader may roll up to maxRolls (3) times
				// subsequent players may only roll as often as the leader did
				if (current == leader && leader.nRolls < maxRolls || current.nRolls < leader.nRolls) {
					current.rollDice();
					roundMsg(current);
				}
				else {
					out.println("Max rolls reached!");
					current = next(current, players);
				}
				// ---- Out --------

			} else if ("n".equals(cmd)) {
				// Process
				// make sure the player rolled before passing
				if (current.nRolls > 0) {
					current = next(current, players);
				} else { out.println("Gotta roll!"); }
			} else {
				out.println("Enter r to roll or n to pass.");
			}

			if (allRolled(players) && current == leader) {
				// --- Process -----
				Player loser = getLoser(players);
				loser.amount -= 1;
				pot += 1;
				if (loser.gameOver()) {
					players = removeLoser(players);
					// passing leader to the next in line, rules do not specify behaviour in case of game over
					leader = next(leader, players);
				} else {
					// loser starts next round
					leader = loser;
				}
				current = leader;
				// resetting dice and nRolls before next round
				for (Player player : players) {
					player.unroll();
				}

				// ----- Out --------------------
				out.println("Round done ... " + loser.name  + " lost!");
				out.println("Next to roll is " + current.name);

				statusMsg(players);
			}
		}
		out.println("Game Over, winner is " + players[0].name + ". Will get " + pot + " from pot");
	}

	// ---- Game logic methods --------------
	// make sure everyone rolled before ending the round
	boolean allRolled(Player[] players) {
		for (Player player : players) {
			if (player.nRolls < 1) { return false; }
		}
		return true;
	}

	// select the next player in line
	Player next(Player current, Player[] players) {
		int nextIndex = indexOf (players, current) + 1;
		if (nextIndex >= players.length) { return players[0]; }
		else { return players[nextIndex]; }
}

	// remove the loser (make sure to check for game over!)
	Player[] removeLoser(Player[] players) {
		Player[] newPlayers = new Player[players.length - 1];
		Player loser = getLoser(players);
		int j = 0;
        for (Player player : players) {
            if (player != loser) {
                newPlayers[j] = player;
                j++;
            }
        }
		return newPlayers;
	}

	// get the loser by comparing scores
	Player getLoser(Player[] players) {
		Player loser = players[0];
		for (Player player : players) {
			if (player.getScore() < loser.getScore()) { loser = player; }
		}
		return loser;
	}

	int indexOf(Player[] players, Player player) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] == player) {
				return i;
			}
		}
		return -1;
	}

	Player getRandomPlayer(Player[] players) {
		return players[rand.nextInt(players.length)];
	}

	// ---------- IO methods (nothing to do here) -----------------------

	Player[] getPlayers() {
		// cleaned this up using a constructor
		Player[] players = new Player[3];
		Player p1 = new Player("Olle");
		Player p2 = new Player("Fia");
		Player p3 = new Player("Lisa");
		players[0] = p1;
		players[1] = p2;
		players[2] = p3;
		return players;
	}

	void statusMsg(Player[] players) {
		out.print("Status: ");
		for (int i = 0; i < players.length; i++) {
			out.print(players[i].name + " " + players[i].amount + " ");
		}
		out.println();
	}

	void roundMsg(Player current) {
		out.println(current.name + " got " + current.fstDice + " and " + current.secDice);
	}

	String getPlayerChoice(Player player) {
		out.print("Player is " + player.name + " > ");
		return sc.nextLine();
	}

	// Possibly useful utility during development
	String toString(Player p) {
		return p.name + ", " + p.amount + ", " + p.fstDice + ", " + p.secDice + ", " + p.nRolls;
	}

	// Class for a player
	class Player {
		String name;
		int amount; // Start amount (money)
		int fstDice; // Result of first dice
		int secDice; // Result of second dice
		int nRolls; // Current number of rolls

		Player(String name) {
			this.name = name;
			amount = startAmount;
			nRolls = 0;
		}

		Player() {
			name = "";
			amount = startAmount;
			nRolls = 0;
		}

		void unroll() {
			nRolls = 0;
			fstDice = 0;
			secDice = 0;
		}

		// rolling two 6-sided dice
		void rollDice() {
			fstDice = rand.nextInt(1, 7);
			secDice = rand.nextInt(1, 7);
			nRolls += 1;
		}

		int getScore() {
			int dice1 = fstDice;
			int dice2 = secDice;
			if ((dice1 == 2 && dice2 == 1) || (dice1 == 1 && dice2 == 2)) {
				return mexico;
			} else if (dice1 == dice2) {
				// adding 65 as this is the maximum score without doubles (or mexico)
				return dice1 + 65;
			} else if (dice1 > dice2) {
				return dice1 * 10 + dice2;
			}
			return dice2 * 10 + dice1;
		}

		boolean gameOver() {
			return amount == 0;
		}
	}

	/**************************************************
	 * Testing
	 *
	 * Test are logical expressions that should evaluate to true (and then be
	 * written out) No testing of IO methods Uncomment in program() to run test
	 * (only)
	 ***************************************************/
	void test() {
		// A few hard coded player to use for test
		// NOTE: Possible to debug tests from here, very efficient!
		Player[] ps = { new Player(), new Player(), new Player() };
		ps[0].fstDice = 2;
		ps[0].secDice = 6;
		ps[1].fstDice = 6;
		ps[1].secDice = 5;
		ps[2].fstDice = 1;
		ps[2].secDice = 1;

		// out.println(getScore(ps[0]) == 62);
		// out.println(getScore(ps[1]) == 65);
		// out.println(next(ps, ps[0]) == ps[1]);
		// out.println(getLoser(ps) == ps[0]);

		exit(0);
	}

}
