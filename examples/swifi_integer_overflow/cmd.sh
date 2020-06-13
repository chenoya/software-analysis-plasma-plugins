cd ../../../plasmalab-1.4.4/ || exit
java -jar libs/fr.inria.plasmalab.terminal-1.4.4.jar  launch \
    -m ../software-analysis-plasma-plugins/examples/swifi_integer_overflow/swifi_integer_overflow_model.toml:software-simulator \
    -r ../software-analysis-plasma-plugins/examples/swifi_integer_overflow/swifi_integer_overflow_req.toml:software-bltl-checker \
    -a swifi-generator \
    -A"Python path"="python3" \
    -A"SWIFI path"="../swifi-tool/swifitool/faults_inject.py" \
    -A"Max simul"="" \
    -A"NOP"="" \
    -A"FLP"="" \
    -A"Z1B"=1 \
    -A"Z1W"="" \
    -A"Other parameters"="-a x86"
