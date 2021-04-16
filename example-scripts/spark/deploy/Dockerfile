FROM docker.io/andreatp/spark-aggregation:0.0.1-2-22325a0f

USER root
RUN cp -r /opt/cloudflow/* /opt/spark/work-dir

RUN chown cloudflow:root /mnt
RUN chmod 775 /mnt
RUN chmod g+w /mnt
USER 185

ENTRYPOINT [ "bash", "/opt/spark-entrypoint.sh" ]
