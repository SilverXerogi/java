FROM tomcat:11.0.0-jdk17

LABEL maintainer="Maksim"

# Удаляем дефолтные приложения Tomcat (по желанию)
RUN rm -rf $CATALINA_HOME/webapps/docs \
    && rm -rf $CATALINA_HOME/webapps/examples \
    && rm -rf $CATALINA_HOME/webapps/host-manager \
    && rm -rf $CATALINA_HOME/webapps/manager

# Копируем WAR файл из target/ (предполагается, что он уже собран Maven)
# Путь относительно директории, где находится Dockerfile (корень проекта)
COPY target/bookstore-app-1.0-SNAPSHOT.war $CATALINA_HOME/webapps/

# Открываем порт 8080
EXPOSE 8080

# Запускаем Tomcat
CMD ["catalina.sh", "run"]