package com.music.android.my_music;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by sulemanshakil on 11/13/16.
 */


public class Tree<T> {
    Node<T> root;


    public Tree(T rootData) {
        root = new Node<T>();
        root.data = rootData;
        root.children = new ArrayList<Node<T>>();
    }

    public static class Node<T> {
        T data;
        ArrayList<Song> songsInNode;
        Node<T> parent;
        List<Node<T>> children;

        public String toString(){
            return (String)data;
        }
    }

    public Node<T> addchild(T child, Node<T> parent){

        Node<T> childNode = new Node<T>();
        childNode.data = child;
        childNode.songsInNode=new ArrayList<Song>();
        childNode.parent = parent;
        parent.children.add(childNode);
        childNode.children =  new ArrayList<Node<T>>();
        return childNode;
    }

    public Node<T> findNode(String string, Node<T> node){
        if (node.toString().equals(string))
            return node;
        List<Node<T>> children = node.children;
        Node<T> res = null;
        for (int i = 0; res == null && i < children.size(); i++) {
            res = (Node<T>) findNode(string, children.get(i));
        }
        return (Node<T>) res;

    }

    public boolean findInChild(String string, Node<T> node){
        List<Node<T>> children = node.children;
        Node<T> res = null;
        for (int i = 0; res == null && i < children.size(); i++) {
            if (node.children.get(i).toString().equals(string))
                return true;
        }
        return false;
    }

    public void traverse(Node<T> child){ // pre order traversal
     //   Log.e("Path",child.toString());
        if(child.songsInNode!=null) {
            for (Song song : child.songsInNode) {
            //    Log.e("Song path", song.getData());
            }
        }
        for(Node<T> each : child.children){
            traverse(each);
        }
    }

}

