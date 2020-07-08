binary_strings = []
decimal_val= []
p = 0
pow_16 = 2**16

def get_poke_command(pos,val):
    return "do Memory.poke(memAddress+{p}, {v});\n".format(p = pos, v = val)

def get_return_command():
    return "return;"

def get_initialization_command():
    return "var int memAddress;\nlet location = getLocation();\nlet memAddress = 16384+location;\n"

def preprocess_file(name):
    with open(name, "r") as f:
           prev = f.readlines()
    for line in prev:
        if line.strip().startswith("do"):

            decimal_val.append(int(line[line.find(",")+1 : line.find(")")].strip()))
            if decimal_val[-1] < 0: 
                bin_eq = bin(pow_16-abs(decimal_val[-1]))[2:]
            else : 
                bin_eq = bin(decimal_val[-1])[2:]
                bin_eq = '0' * (16 - len(bin_eq)) + bin_eq
            print(bin_eq[::-1])
            binary_strings.append(bin_eq[::-1])      
    print()


def get_horizantal_expansion(ele, hor, p):
    _expanded = "".join([e*hor for e in ele])
    print(_expanded)
    _expanded = [_expanded[16*i:16*(i+1)] for i in range(len(_expanded)//16)]
    code = ""
    for jmp,ele in enumerate(_expanded):
        if ele[-1] == '1': s=pow_16 #2**16
        else: s=0
        num = int(ele[::-1],2) - s
        # print('0'+ele[:15][::-1], end="")
        code+=get_poke_command(p+jmp,num)
    # print(end="\n")
    return code

def expand(hor,ver,ele):
    global p
    code = ""
    for i in range(ver):
        code += get_horizantal_expansion(ele,hor,p)
        p+=32
    return code

def generate_new_function_body(hor, ver):
    res = get_initialization_command()
    for bits in binary_strings:
        res+=expand(hor,ver,bits)
    res+=get_return_command()
    return res

if __name__ == "__main__":
    # hor = int(input("Enter horizantal factor : "))
    # ver = int(input("Enter vertical factor : "))
    # name = input("Enter the file_name of 16x16 string : ")
    preprocess_file("dinosaur_right")  # give me the file of the sprite
    print(generate_new_function_body(2, 3))