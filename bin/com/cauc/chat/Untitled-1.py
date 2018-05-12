strCount=0
a=raw_input("plz input a string:")
for i in a:
if i.isalpha():strCount+=1
print "字母个数为：%d"%(strCount)