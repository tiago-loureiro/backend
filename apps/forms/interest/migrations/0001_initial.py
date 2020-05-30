# Generated by Django 3.0.6 on 2020-05-30 13:04

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = []

    operations = [
        migrations.CreateModel(
            name="InterestSchema",
            fields=[
                ("id", models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("company", models.CharField(max_length=250)),
                ("person", models.CharField(max_length=250)),
                ("email", models.EmailField(max_length=254)),
                ("phone", models.IntegerField()),
                ("day", models.CharField(max_length=10)),
                ("extras", models.CharField(max_length=250)),
                ("banquet", models.BooleanField()),
                ("other", models.TextField()),
                ("confirmation", models.BooleanField()),
            ],
            options={"verbose_name": "Interest form", "verbose_name_plural": "Interest form",},
        ),
    ]