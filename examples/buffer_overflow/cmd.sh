cd ../../../plasmalab-1.4.4/ || exit
java -jar libs/fr.inria.plasmalab.terminal-1.4.4.jar launch \
    -m ../software-analysis-plasma-plugins/examples/buffer_overflow/buffer_overflow_model.toml:software-simulator \
    -r ../software-analysis-plasma-plugins/examples/buffer_overflow/buffer_overflow_req_01.toml:software-bltl-checker \
    -r ../software-analysis-plasma-plugins/examples/buffer_overflow/buffer_overflow_req_02.toml:software-bltl-checker \
    -a montecarlo -A"Total samples"=20