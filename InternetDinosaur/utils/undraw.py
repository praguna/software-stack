def preprocess_file(file):
    res = ""
    with open(file) as f:
        lines = f.readlines()
    for line in lines:
        if line.strip().startswith("do"):
            res+=line[:line.find(",")+1] + "0);\n"
        else:
            res+=line.strip()+"\n"
    return res



if __name__ == "__main__":
    print(preprocess_file("undraw_input"))