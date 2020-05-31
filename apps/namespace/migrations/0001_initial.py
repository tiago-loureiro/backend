# Generated by Django 3.0.6 on 2020-05-31 12:08

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = []

    operations = [
        migrations.CreateModel(
            name="Namespace",
            fields=[
                ("id", models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("title", models.CharField(max_length=100)),
                ("description", models.CharField(max_length=250)),
                ("namespace", models.SlugField(editable=False)),
            ],
        ),
    ]
