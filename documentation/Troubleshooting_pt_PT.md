# Troubleshooting

O Troubleshooting é uma forma de resolução de problemas, frequentemente aplicado para reparar produtos com falhas. É uma procura lógica e sistemática pela fonte de um problema de modo a resolvê-lo e tornar o produto ou processo operacional outra vez.

Nesta secção, encontrará problemas comuns que afetam este produto e potenciais soluções sobre como resolvê-los.

## Erro: Demasiados ficheiros abertos

Às vezes, pode encontrar erros nos logs como:

```
RODA_HOME/logs/roda-wui.log:pt.gov.dgarq.roda.core.common.RODAClientException: Error connecting to Login service - Too many open files
RODA_HOME/logs/roda-wui.log:Caused by: java.net.SocketException: Too many open files
```

Isto pode acontecer quando o servidor tem muitos ficheiros abertos. Para ver quantos ficheiros o servidor tem abertos, adquira o PID do processo, e depois execute lsof | grep <PID> | wc. Em muitos computadores, o número máximo de ficheiros predefinido que um processo pode abrir é baixo (ex. 1024).

Para modificar este limite, edite `/etc/security/limits.conf` adicionando o seguinte:

```
* soft nofile 2048
* hard nofile 2048
```

Isto permitirá que o processo executado por qualquer um tenha 2048 ficheiros abertos. Precisará de reiniciar o computador para aplicar estas alterações. Também pode usar o comando `ulimit` para alterá-lo durante o tempo de execução, mas estas alterações do comando não persistirão no próximo arranque.
