package najeeb.knapsacksolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class KnapsackBranchAndBound {

	public static void main(String[] args) {
		if (args.length == 0) {
			return;
		}
		Scanner sc = null;
		try {
			sc = new Scanner(new FileInputStream(new File(args[0])));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		String[] first_line = sc.nextLine().split(" ");
		items = new Item[Integer.parseInt(first_line[0])];
		capacity = Integer.parseInt(first_line[1]);
		for (int i = 0; i < items.length; i++) {
			String[] line = sc.nextLine().split(" ");
			items[i] = new Item(Integer.parseInt(line[0]), Integer.parseInt(line[1]), i);
		}
		sc.close();
		for (int i = 0; i < items.length; i++) {
			int max = 0;
			for (int j = i; j < items.length; j++)
				if ((double) items[max].value / items[max].weight < (double) items[j].value / items[j].weight)
					max = j;
			Item c = items[i];
			items[i] = items[max];
			items[max] = c;
		}
		top_of_tree = new Branch(new int[items.length], new boolean[items.length], 0, 0, 0);
		int[] best_selection = top_of_tree.solutionAtBranch();
		System.out.println(objFunc(best_selection) + " 1");
		int[] output = new int[items.length];
		for (int i = 0; i < items.length; i++) {
			output[items[i].index] = best_selection[i];
		}
		for (int i = 0; i < output.length; i++) {
			System.out.print(output[i]);
			System.out.print(" ");
		}
	}

	private static Item[] items;
	private static int capacity;
	private static Branch top_of_tree;
	private static int best_objective;

	private static class Item {
		int value;
		int weight;
		int index;

		private Item(int value, int weight, int index) {
			this.value = value;
			this.weight = weight;
			this.index = index;
		}
	}

	private static class Branch {
		int depth;
		int obj;
		int[] selection;
		boolean[] committed;

		private Branch(int[] selection, boolean[] committed, int total_weight, int depth, int obj) {
			this.obj = obj;
			this.depth = depth;
			this.selection = selection;
			this.committed = committed;
			this.total_weight = total_weight;

			for (int i = depth; i < items.length; i++) {
				if (selection[i] == 0 && capacity - total_weight < items[i].weight) {
					this.committed[i] = true;
				}
			}
			int obj_val = objFunc(selection);
			if (obj_val > best_objective) {
				best_objective = obj_val;
			}
		}

		private float optimisticEvaluation() {
			int filled = total_weight;
			int sum = obj;
			int i;
			for (i = depth; i < items.length && filled + items[i].weight <= capacity; i++) {
				if (selection[i] == 1 || !committed[i]) {
					sum += items[i].value;
					filled += items[i].weight;
				}
			}
			if (i == items.length)
				return sum;
			return sum + items[i].value * (float) (capacity - filled) / items[i].weight;
		}

		boolean bounded;
		Branch l;
		Branch r;
		int total_weight;

		private void expand() {
			int next_item = -1;
			for (int i = depth; i < items.length; i++) {
				if (!committed[i]) {
					next_item = i;
					break;
				}
			}
			if (next_item == -1 || optimisticEvaluation() <= best_objective) {
				bounded = true;
				return;
			}
			boolean[] next_committed = committed.clone();
			next_committed[next_item] = true;
			int[] selection_l = selection.clone();
			selection_l[next_item] = 1;
			int[] selection_r = selection.clone();

			l = new Branch(selection_l, next_committed.clone(), total_weight + items[next_item].weight, next_item + 1,
					obj + items[next_item].value);
			r = new Branch(selection_r, next_committed, total_weight, next_item + 1, obj);

		}

		int[] solutionAtBranch() {
			expand();
			if (!bounded) {
				int[] to_return = betterOf(l.solutionAtBranch(), r.solutionAtBranch());
				l = null;
				r = null;
				return to_return;
			} else
				return selection;
		}

	}

	private static int[] betterOf(int[] s1, int[] s2) {
		if (objFunc(s1) > objFunc(s2))
			return s1;
		else
			return s2;
	}

	private static int objFunc(int[] selection) {
		int sum = 0;
		for (int i = 0; i < items.length; i++) {
			sum += selection[i] * items[i].value;
		}
		return sum;
	}

}
