#define SECRET_CODE 4242

volatile int one = 1;

int main() {
    if (one == 0) 
    {
        return SECRET_CODE;
    }
    else
    {
        return 0;
    }
}

