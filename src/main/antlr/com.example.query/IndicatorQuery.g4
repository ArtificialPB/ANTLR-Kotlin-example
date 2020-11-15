grammar IndicatorQuery;

@lexer::header {
    package com.example.query;
}

@parser::header {
    package com.example.query;
}

/* Parser rules */

indicator : indicatorName GROUP_START parameter* GROUP_END;

indicatorName: WORD;

parameter : (indicator | BOOLEAN | WORD | NUMBER);

/* Lexer rules */

fragment LETTER     : [a-zA-Z] ;
fragment DIGIT      : [0-9];

BOOLEAN             : 'true' | 'false' ;
WORD                : (LETTER | '_' | '-' | DIGIT)+ ;
NEWLINE             : ('r'? 'n' | 'r')+ ;
NUMBER              : DIGIT+ ([.] DIGIT+)? ;

IGNORED             : (' ' | ',') -> skip;
GROUP_START         : '(';
GROUP_END           : ')';
