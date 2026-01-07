FROM gitpod/workspace-full:latest

# Java 21のインストール
RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && \
    sdk install java 21.0.1-tem && \
    sdk default java 21.0.1-tem"

# 環境変数の設定
ENV JAVA_HOME=/home/gitpod/.sdkman/candidates/java/current
ENV PATH=$JAVA_HOME/bin:$PATH

# Mavenのバージョン確認と更新（必要な場合）
RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && \
    sdk install maven 3.9.6 || true && \
    sdk default maven 3.9.6 || true"

# PostgreSQLクライアントツールのインストール
RUN sudo apt-get update && \
    sudo apt-get install -y postgresql-client && \
    sudo rm -rf /var/lib/apt/lists/*

# ワークスペースのディレクトリ設定
WORKDIR /workspace
