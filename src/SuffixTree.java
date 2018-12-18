/*
        Zadanie 3
        Podlancuch Alfa nazywamy powtorzonym prefiksem lancucha S, jesli Alfa jest prefiksem
        S oraz ma on postac BetaBeta dla pewnego lancucha Beta. Zaproponuj i zaimplementuj
        liniowy algorytm znajdujacy najdluzszy powtorzony prefiks dla zadanego lancucha S.
        */

//Definicje:
//s - tekst, ciąg symboli s = s1,s2,...sn nalezacych do alfabetu
//n - Dlugosc tekstu (liczba jego elementów)
//p - pattern (wzorzec)

import java.util.*;


public class SuffixTree {

    private static CharSequence s = "";

    public static class Node {

        int begin;
        int end;
        int depth; //distance in characters from root to this node
        Node parent;
        Node suffixLink;
        Integer suffixBegin;
        Map<Character, Node> children;  //zamiast Node[] children

        //Sluzy do wyznaczenia liczby osiagalnych wierzcholkow reprezentujacych sufiksy dla kazdego wezla
        // drzewa sufiksowego
        int numberOfLeaves;             //zliczamy liscie

        Node(int begin, int end, int depth, int noleaf, Node parent) {

            this.begin = begin;
            this.end = end;
            this.depth = depth;
            this.parent = parent;

            children = new HashMap<>();
            numberOfLeaves = noleaf;


        }
    }

    private static Node buildSuffixTree(CharSequence s) {

        SuffixTree.s = s;

        int n = s.length();

        Node root = new Node(0, 0, 0, 0, null);
        Node node = root;

        for (int i = 0, tail = 0; i < n; i++, tail++) {

            //ustaw ostatni stworzony wezel wewnętrzny na null przed rozpoczeciem kazdej fazy.
            Node last = null;

            while (tail >= 0) {
                Node ch = node.children.get(s.charAt(i - tail));
                while (ch != null && tail >= ch.end - ch.begin) {

                    //liscie
                    node.numberOfLeaves++;

                    tail -= ch.end - ch.begin;
                    node = ch;
                    ch = ch.children.get(s.charAt(i - tail));
                }

                if (ch == null) {
                    // utworz nowy Node z biezacym znakiem
                    node.children.put(s.charAt(i),
                            new Node(i, n, node.depth + node.end - node.begin, 1, node));

                    //liscie
                    node.numberOfLeaves++;

                    if (last != null) {
                        last.suffixLink = node;
                    }
                    last = null;
                } else {
                    char t = s.charAt(ch.begin + tail);
                    if (t == s.charAt(i)) {
                        if (last != null) {
                            last.suffixLink = node;
                        }
                        break;
                    } else {
                        Node splitNode = new Node(ch.begin, ch.begin + tail,
                                node.depth + node.end - node.begin, 0, node);
                        splitNode.children.put(s.charAt(i),
                                new Node(i, n, ch.depth + tail, 1, splitNode));

                        //liscie
                        splitNode.numberOfLeaves++;

                        splitNode.children.put(t, ch);

                        //liscie
                        splitNode.numberOfLeaves += ch.numberOfLeaves;

                        ch.begin += tail;
                        ch.depth += tail;
                        ch.parent = splitNode;
                        node.children.put(s.charAt(i - tail), splitNode);

                        //liscie
                        node.numberOfLeaves++;

                        if (last != null) {
                            last.suffixLink = splitNode;
                        }
                        last = splitNode;
                    }
                }
                if (node == root) {
                    --tail;
                } else {
                    node = node.suffixLink;
                }
            }
        }
        return root;
    }


    private static void print(CharSequence s, int i, int j) {
        for (int k = i; k < j; k++) {
            System.out.print(s.charAt(k));
        }
    }

    //Jesli chcemy wydrukowac drzewo nalezy odkomentowac w main
    private static void printTree(Node n, CharSequence s, int spaces) {
        int i;
        for (i = 0; i < spaces; i++) {
            System.out.print("␣");
        }
        print(s, n.begin, n.end);
        System.out.println("␣" + (n.depth + n.end - n.begin)+ " leaf: " + n.numberOfLeaves);

        for (Node child : n.children.values()) {
            if (child != null) {
                printTree(child, s, spaces + 4);
            }
        }

    }

    private static int[] table;
    private static CharSequence result = "";

    public static void main(String[] args) {/**/
        String text = "alaalamact!";
        table = new int[text.length()];

        Node root = buildSuffixTree(text);
        printTree(root, text, 0);
        System.out.println(searchPreffix(root));
    }

    public static CharSequence searchPreffix(Node root) {
        Node currentNode = searchLastLeaf(root, s);
        while (result.length() == 0) {
            currentNode = readFirstLeafFromSubTree(currentNode);
            if (currentNode.parent == root) {
                return result;
            }
            currentNode = readChildrenFromParent(currentNode.parent);
        }
        return result;
    }


    public static Node searchLastLeaf(Node root, CharSequence text) {
        Node lastLeaf = root;
        int positionInText = 0;
        while (positionInText < text.length()) {
            lastLeaf = lastLeaf.children.get(text.charAt(positionInText));
            positionInText = lastLeaf.end;
        }
        return lastLeaf;
    }

    public static Node readFirstLeafFromSubTree(Node leaf) {
        if (leaf.suffixBegin != null) {
            table[leaf.suffixBegin] = 1;
        }
        int i;
        if (leaf.end == s.length()) {
            i = leaf.end - 1;
        } else {
            i = leaf.end;
        }
        for (; i >= leaf.begin; i--) {
            if (table[i] == 1) {
                if (i * 2 + s.length() - leaf.end * 2 <= s.length()) {
                    result = s.subSequence(0, i);
                }
                return leaf;
            }
        }
        return leaf;
    }

    public static Node readChildrenFromParent(Node node) {
        Collection<Node> values = node.children.values();
        Collection<Node> toRemove = new ArrayList<>();
        int begin = 0;
        for (Node child : values) {
            if (child.begin > begin) {
                begin = child.begin;
            }
            if (!child.children.isEmpty()) {
                readChildrenFromParent(child);
            }
            if (child.suffixBegin != null) {
                table[child.suffixBegin] = 1;
            }
            int i;
            if (child.end == s.length()) {
                i = child.end - 1;
            } else {
                i = child.end;
            }
            for (; i >= child.begin; i--) {
                if (table[i] == 1 && node.end != 0) {
                    if (i * 2 + s.length() - node.end * 2 <= s.length() ) {
                        result = s.subSequence(0, i);
                        return node;
                    }
                } else if (table[i] == 1 && node.begin == 0 && node.end == 0) {
                    if (i * 2 + s.length() - node.end * 2 <= s.length() ) {
                        result = s.subSequence(0, i);
                        return node;
                    }

                }
            }
            toRemove.add(child);
        }
        values.removeAll(toRemove);

        return node;
    }

}

