import hashlib
import time
import argparse


class PasswordCracker(object):
        
        
    def hashcode(self, code):
        hl = hashlib.md5()
        hl.update(code.encode(encoding='utf-8'))
        return hl.hexdigest()
            
            
    def get_char_list(self, ):
        res = []
        for c in range(ord("a"), ord("z")+1):
            res.append(chr(c))
    #     for c in range(ord("A"), ord("Z")+1):
    #         res.append(chr(c))
        return res
    
    
    def crack(self, password, l, r):
        res = self.find_code(password, l, r)
        return res

    
    def update_one_num(self, code, pos, num):
        code[pos] = num
        return code
    
    def find_code(self, password, l, r):
        seq = self.get_char_list()
        code_init = [seq[0] for _ in range(5)]
        code = code_init[:]
        count = 0
        is_first = True
        for i in range(len(seq)):
            code = self.update_one_num(code, 0, seq[i])
            for j in range(len(seq)):
                code = self.update_one_num(code, 1, seq[j])
                for k in range(len(seq)):
                    code = self.update_one_num(code, 2, seq[k])
                    for x in range(len(seq)):
                        code = self.update_one_num(code, 3, seq[x])
                        for y in range(len(seq)):
                            code = self.update_one_num(code, 4, seq[y])
                            code_str = "".join(code)
                            count += 1
                            if count < l:
                                continue
                            if is_first:
                                print("start code: {}".format(code_str))
                                is_first = False
                            if self.hashcode(code_str) == password:
                                return "find code: {}".format(code_str)
                            if count > r:
                                return "not found, end code: {}".format(code_str)
        return "not found, end code: {}".format(code_str)


if __name__ == '__main__':
    aparser = argparse.ArgumentParser()
    aparser.add_argument('--password', default="be71a0e7283cee0c0c169f5005941ac6")
    aparser.add_argument('--left', default=0, type=int)
    aparser.add_argument('--right', default=1000000, type=int)
    flags = aparser.parse_args()

    password_cracker = PasswordCracker()
    print(flags.password, flags.left, flags.right)
    res = password_cracker.crack(flags.password, flags.left, flags.right)
    print(res)