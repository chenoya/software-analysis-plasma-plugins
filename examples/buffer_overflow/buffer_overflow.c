#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>

/*
0x..dec0  buf[0]
0x..dec1  buf[1]
0x..dec2  buf[2]
0x..dec3  buf[3]
0x..dec4  buf[4]
0x..dec5  buf[5]
0x..dec6  buf[6]
0x..dec7  buf[7]
0x..dec8  
0x..dec9  
0x..deca  
0x..decb  
0x..decc  |        0x64
0x..decd  |--> x = 0x0
0x..dece  |        0x0
0x..decf  |        0x0
*/

/*
compiled with : gcc -g -o buffer_overflow buffer_overflow.c -fno-stack-protector
*/

int main(int argc, char **argv) {
    int x = 42;
    char buf[8];
    memset(buf, '\0', sizeof(buf));
    srand(time(NULL));
    int r = rand() % 14; //overflow on x when r = 13   //no terminating \0 when r >= 7
    printf("%d\n", r);
    strncpy(buf, "dddddddddddddddddddddddddddddddd", r);
    if (x == 100) {
        printf("secret");
    } else {
        printf("%s\n", buf);
    }
    return 0;
}
