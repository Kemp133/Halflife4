package com.halflife3.Mechanics.AI;

import com.halflife3.GameUI.Maps;
import com.halflife3.Mechanics.Vector2;
import javafx.scene.image.*;
import javafx.scene.paint.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;

public class AI {
	private final int      BLOCK_SIZE = 40;
	private       Node[][] map        = null;
	private       int      mapWidth;
	private       int      mapHeight;

	public boolean setupMap() {
		try {
			Image mapImage = new Image(new FileInputStream(Maps.Map));
			mapWidth  = (int) mapImage.getWidth();
			mapHeight = (int) mapImage.getHeight();

			map = new Node[mapHeight][mapWidth];

			PixelReader px = mapImage.getPixelReader();

			for (int i = 0; i < mapHeight; i++) {
				for (int j = 0; j < mapWidth; j++) {
					Color color = px.getColor(j, i);
					map[i][j] = new Node(new Vector2((i * BLOCK_SIZE), (j * BLOCK_SIZE)));
					if (color.equals(Color.BLACK))
						map[i][j].type = "Wall";
				}
			}

			for (int i = 1; i < mapHeight - 1; i++) {
				for (int j = 1; j < mapWidth - 1; j++) {
					map[i][j].addChild(map[i][j + 1]);
					map[i][j].addChild(map[i + 1][j]);
					map[i][j].addChild(map[i][j - 1]);
					map[i][j].addChild(map[i - 1][j]);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file at location: 'res/map.png'");
			return false;
		}

		return true;
	}

	public Vector2 getNextPos(Vector2 bot, Vector2 toGoTo) {
		Node botNode = map[(int) (bot.getY()) / BLOCK_SIZE][(int) (bot.getX()) / BLOCK_SIZE];
		Node endNode = map[(int) (toGoTo.getY()) / BLOCK_SIZE][(int) (toGoTo.getX()) / BLOCK_SIZE];

		if (botNode.position.equals(endNode.position))
			return bot;

		Node nextNode = aStar(endNode, botNode).get(0);

		resetParents();

		return new Vector2(nextNode.position.getY(), nextNode.position.getX());
	}

	private ArrayList<Node> aStar(Node Start, Node End) {
		ArrayList<Node>  openList       = new ArrayList<>();
		ArrayList<Node>  closedList     = new ArrayList<>();
		ArrayList<Node>  pathEndToStart = new ArrayList<>();
		Node             q;
		boolean          keepSearching  = true;
		boolean          skipSuccessor;
		Comparator<Node> compareByF     = (Node n1, Node n2) -> (int) (n1.f - n2.f);

		Start.f = Start.h =
				Math.abs(Start.position.getX() - End.position.getX()) + Math.abs(Start.position.getY() - End.position.getY());

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
				} else
					child.setParent(q);
				//endregion

				//region If successor is the goal, stop search
				if (child.position.equals(End.position)) {
					keepSearching = false;
					q             = child;
					break;
				}
				//endregion

				//region Calculate successor's g, h and f
				if (!child.type.equals("Wall")) {
					child.g = q.g + BLOCK_SIZE;
					child.h =
							Math.abs(child.position.getX() - End.position.getX()) + Math.abs(child.position.getY() - End.position.getY());
					child.f = child.g + child.h;
				} else
					continue;
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

				if (!skipSuccessor)
					openList.add(child);
			}

			closedList.add(q);
		}

		q = closedList.remove(closedList.size() - 1);

		pathEndToStart.add(q.getParent());
		return pathEndToStart;

		/*while (!q.position.equals(Start.position)) {
			q = q.getParent();
			pathEndToStart.add(q);
		}

		return pathEndToStart;*/
	}

	private void resetParents() {
		for (int i = 0; i < mapHeight; i++)
			for (int j = 0; j < mapWidth; j++)
			     map[i][j].setParent(null);
	}
}
