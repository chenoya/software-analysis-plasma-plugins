.text
.global _start

_start:
      movl    $2,%eax
      movl    $1,%ebx
      sub     %eax, %ebx
myexit:
	  movl    $0,%ebx             # first argument: exit code
	  movl    $1,%eax             # system call number (sys_exit)
	  int     $0x80               # call kernel

// compiled with
// as -g -o integer_overflow.o integer_overflow.S
// ld -o integer_overflow integer_overflow.o
