package com.example.project;

public class Course {
    public int id;
    public String title;
    public String price;
    public String rating;
    public String imageName;
    public Course() {}
    public Course(int id, String title, String price, String rating, String imageName) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.rating = rating;
        this.imageName = imageName;
    }
}
