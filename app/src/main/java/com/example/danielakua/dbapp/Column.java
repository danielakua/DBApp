package com.example.danielakua.dbapp;

class Column
{
    private String _name;// keeps name of column
    private String _type;// keeps type of column
    private String _value = "";// keeps value of column

    public Column(){}

    public Column(String name, String type) {
        _name = name;
        _type = type;
    }

    public String get_name() { return _name; }

    public String get_type() { return _type; }

    public String get_value() { return _value; }

    public void set_name(String name) { _name = name; }

    public void set_type(String type) { _type = type; }

    public void set_value(String value) { _value = value; }
}
