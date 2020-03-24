package com.halflife3.Mechanics.AI;

import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.Networking.Server.ClientListServer;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;

public class AI {
	private final int ONE_MOVE = 40;
	private Node[][] map = null;

	public boolean setupMap() {
		try {
			Image mapImage = new Image(new FileInputStream("res/map.png"));
			int width = (int) mapImage.getWidth();
			int height = (int) mapImage.getHeight();

			map = new Node[height][width];

			PixelReader px = mapImage.getPixelReader();

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					Color color = px.getColor(j,i);
					map[i][j] = new Node(new Vector2((i * ONE_MOVE), (j * ONE_MOVE)));
					if (color.equals(Color.BLACK))
						map[i][j].type = "Wall";
				}
			}

			for (int i = 1; i < height - 1; i++) {
				for (int j = 1; j < width - 1; j++) {
					map[i][j].addChild(map[i][j+1]);
					map[i][j].addChild(map[i+1][j]);
					map[i][j].addChild(map[i][j-1]);
					map[i][j].addChild(map[i-1][j]);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file at location: 'res/map.png'");
			return false;
		}

		return true;
	}

	public PositionPacket getBotMovement(PositionPacket bot) {
		Vector2 toGoTo = whereToGo(bot);

		Node botNode = map[(int) (bot.orgPosY) / 40][(int) (bot.orgPosX) / 40];
		Node endNode = map[(int) (toGoTo.getY()) / 40][(int) (toGoTo.getX()) / 40];

		System.out.println(botNode);
		System.out.println("Target bot: " + endNode);
		System.out.println("Path taken:  Start: " + bot.orgPosX + "|" + bot.orgPosY);
		for (Node step : aStar(endNode, botNode))
			System.out.println("    " + step);

		return bot;
	}

	private ArrayList<Node> aStar(Node Start, Node End) {
		ArrayList<Node> openList = new ArrayList<>();
		ArrayList<Node> closedList = new ArrayList<>();
		ArrayList<Node> pathEndToStart = new ArrayList<>();
		Node q;
		boolean keepSearching = true;
		boolean skipSuccessor;
		Comparator<Node> compareByF = (Node n1, Node n2) -> (int) (n1.f - n2.f);

		Start.f = Start.h = Math.abs(Start.position.getX() - End.position.getX()) +
				Math.abs(Start.position.getY() - End.position.getY());

		openList.add(Start);

		while (!openList.isEmpty() && keepSearching) {
			openList.sort(compareByF);
			q = openList.remove(0);

			for (Node child : q.children) {
				skipSuccessor = false;
				//region Set successor's parent as q
				if (q.getParent() != null && !openList.isEmpty()) {
					if (!q.getParent().position.equals(child.position))
						child.setParent(q);
				} else child.setParent(q);
				//endregion

				//region If successor is the goal, stop search
				if (child.position.equals(End.position)) {
					keepSearching = false;
					q = child;
					break;
				}
				//endregion

				//region Calculate successor's g, h and f
				if (!child.type.equals("Wall")) {
					child.g = q.g + ONE_MOVE;
					child.h = Math.abs(child.position.getX() - End.position.getX()) +
							Math.abs(child.position.getY() - End.position.getY());
					child.f = child.g + child.h;
				} else continue;
				//endregion

				//region If a lower f node with successor's position already exists in openList - skip successor
				if (!openList.isEmpty()) {
					for (Node temp : openList) {
						if (temp.position.equals(child.position) && temp.f <= child.f) {
							skipSuccessor = true;
							break;
						}
					}
				}
				//endregion

				//region If a lower f node with successor's position already exists in closedList - skip successor
				if (!closedList.isEmpty() && !skipSuccessor) {
					for (Node temp : closedList) {
						if (temp.position.equals(child.position) && temp.f <= child.f) {
							skipSuccessor = true;
							break;
						}
					}
				}
				//endregion

				if (!skipSuccessor) openList.add(child);
			}

			closedList.add(q);
		}

		q = closedList.remove(closedList.size() - 1);

		while (!q.position.equals(Start.position)/* && pathEndToStart.size() < 3*/) {
			q = q.getParent();
			pathEndToStart.add(q);
		}

		return pathEndToStart;
	}

	private Vector2 whereToGo(PositionPacket bot) {
		Vector2 toGoTo = new Vector2();

		//region Selects the closest enemy to go to next
		var positions = ClientListServer.positionList.entrySet();
		double shortestDistance = Double.MAX_VALUE;
		for (var enemyEntry : positions) {
			PositionPacket enemyPacket = enemyEntry.getValue();
			if (enemyPacket.orgPosX == bot.orgPosX && enemyPacket.orgPosY == bot.orgPosY)
				continue;

			double distance = Math.pow(bot.orgPosX - enemyPacket.orgPosX, 2) +
					Math.pow(bot.orgPosY - enemyPacket.orgPosY, 2);

			if (distance < shortestDistance) {
				shortestDistance = distance;
				toGoTo.setX(enemyPacket.orgPosX);
				toGoTo.setY(enemyPacket.orgPosY);
			}
		}
		//endregion

		//region Selects the center of the map
		toGoTo = new Vector2(25*40, 15*40);
		//endregion

		return toGoTo;
	}

}