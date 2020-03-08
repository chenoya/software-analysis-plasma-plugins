#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char **argv) {
    int x = 2000000000;
    srand(time(NULL));
    int y = rand() % 200000000;
    x += y;
    printf("%d\n", x);
    if (x < 0) {
        printf("secret");
    } else {
        printf("hidden");
    }
    return 0;
}
