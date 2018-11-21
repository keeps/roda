# Installation

This is the easiest way to install RODA. If you want to test drive the software, just follow these instructions based on your operating system. We support MacOS, Windows and Linux.

## MacOS and Windows

Just install [Kitematic](https://kitematic.com), and search for "roda". Install and run docker container. It's that easy.

Kitematic is like an AppStore that automates the Docker installation and setup process and provides an intuitive graphical user interface (GUI) for running Docker containers (i.e. lightweight Virtual Machines).

![Search and install](images/kitematic_search.png "Search and install RODA in Kitematic")

![Open](images/kitematic_open.png "Open RODA in Kitematic")

**NOTE**: The RODA docker container has some limitations on Windows due to filename incompatibilities. This means that your will be limited to the storage capacity within the container. If you change the default configuration to use the storage of the host machine it will not work.



## Linux

On Linux, follow these instructions:

1. Install docker for your system: https://docs.docker.com/engine/installation/
2. Pull or update to the latest roda container, on the command line run:  `sudo docker pull keeps/roda`
3. Run the container:
  * RODA 3 `docker run -p 8080:8080 -v ~/.roda:/roda keeps/roda`
  * RODA 2 `docker run -p 8080:8080 -v ~/.roda:/root/.roda keeps/roda`
4. Access RODA on your browser: [http://localhost:8080](http://localhost:8080)

NOTE: the docker commands will need `sudo` before if your user does not belong to the `docker` group.

NOTE 2: if one wants to run some cronjobs in RODA container, there're two possibilities. But before, lets create a cronjob file with the right permissions (chmod 644 ~/cronjob ; sudo chown root.root ~/cronjob). In this example we are updating, at 2 am, siegfried signature file:

```
MAILTO=""
0 2 * * * root sf -update && pkill sf
```

And the two possibilities are:

Add cronjob file via docker copy (after starting the container)

```bash
$> docker cp ~/cronjob CONTAINER_NAME:/etc/cron.d/cronjob
```

Or run the container using the volume option (-v) to pass by the cronjob file:

```bash
$> docker run -p 8080:8080 -v ~/.roda:/root/.roda -v ~/cronjob:/etc/cron.d/cronjob keeps/roda
```

To start as a service you can install supervisord and create the file `/etc/supervisor/conf.d/roda.conf` with:

```
[program:roda]
command=docker run ... (see RODA version specific command above)
directory=/tmp/
autostart=true
autorestart=true
startretries=3
stderr_logfile=/var/log/supervisor/roda.err.log
stdout_logfile=/var/log/supervisor/roda.out.log
user=roda
```

These are the steps to start `supervisord`:

1. Create user 'roda': `sudo adduser roda`
2. Add user 'roda' to 'docker' group: `sudo usermod -aG docker roda`
3. Then restart supervisord (`sudo service supervisord restart`)


# Going into production

Every production environment is tailored for the specific requirements of the repository to be made available.

Consideration on data volume (size, number of object, heterogeneity of formats), infrastructure type, designated community, high availability requirements, systems to integrated and so forth need to be taken into account when designing  the architecture to put into production.

Many of the production environments we have made involve the use of docker containers, as shown in this instructions. A startup example is available [here](https://github.com/keeps/roda/blob/master/deploys/cloud/docker-compose.yaml).

If you need help scaling your system into production, we provide commercial services for installation, maintenance, support, training, data migration, system integration, custom development and digital preservation consultancy. Please contact [sales@keep.pt](mailto:sales@keep.pt).
