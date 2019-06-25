import os
from tabula import wrapper
tables = wrapper.read_pdf("tablehtml.pdf",multiple_tables=True,pages='all')

i=1
for table in tables:
    table.to_excel('output'+str(i)+'.xlsx',index=False)
    print(i)
    i=i+1