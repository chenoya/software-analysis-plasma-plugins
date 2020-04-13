grammar MIOutput;


output: out_of_band_record* result_record? out_of_band_record* '(gdb)' ' '? NL;

result_record: (Token_)? '^' result_class ( ',' result )* NL;

out_of_band_record: async_record | stream_record;

async_record: exec_async_output | status_async_output | notify_async_output;

exec_async_output: Token_? '*' async_output NL;

status_async_output: Token_? '+' async_output NL;

notify_async_output: Token_? '=' async_output NL;

async_output: async_class ( ',' result )*;

result_class: DONE | RUNNING | CONNECTED | ERROR | EXIT;

//Async_class: 'stopped'; //| others
async_class: String | result_class;

result: variable '=' value;

variable: String;

value: const_ | tuple | list;

const_: C_string;

tuple: '{}' | '{' result ( ',' result )* '}';

list: '[]' | '[' value ( ',' value )* ']' | '[' result ( ',' result )* ']';

stream_record: console_stream_output | target_stream_output | log_stream_output;

console_stream_output: '~' C_string NL;

target_stream_output: '@' C_string NL;

log_stream_output: '&' C_string NL;

DONE: 'done';
RUNNING: 'running';
CONNECTED: 'connected';
ERROR: 'error';
EXIT: 'exit';

NL: '\n' | '\r\n';

Token_: ('0' .. '9')+;

String: ('a' .. 'z' | '-')+;

C_string: '"' ( EscapeSeq | ~["\r\n\\] )* '"';

EscapeSeq: '\\' ('\'' |'"'| '?'| 'a' |'b' |'f'| 'n'| 'r'| 't'| 'v'| '\\');
