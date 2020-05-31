import re
from typing import Callable, Dict

from rest_framework import parsers, renderers


def to_camel_case(snake_case_str: str) -> str:
    """
    Transforms snake_case to camelCase
    """
    components = snake_case_str.split("_")
    titled_components = "".join(x.title() for x in components[1:])

    return f"{components[0]}{titled_components}"


def to_snake_case(camel_case_str: str) -> str:
    """
    Transforms camelCase to snake_case
    """
    return re.sub("([A-Z])([a-z0-9]+)", r"_\1\2", camel_case_str).lower()


def deep_case_transform(data: Dict, to_case_func: Callable):
    """
    Recursively convert a dictionary from snake_case to camelCase.
    """
    transformed_data = {}

    if data is None:
        return

    if isinstance(data, list):
        new_data = []
        for obj in data:
            if isinstance(obj, dict):
                new_data.append(deep_case_transform(obj, to_case_func))
            else:
                new_data.append(obj)

        return new_data

    for key, value in data.items():
        transformed_key = to_case_func(key)
        if not isinstance(value, dict) and not isinstance(value, list):
            transformed_data[transformed_key] = value

        if isinstance(value, list):
            new_value = []
            for obj in value:
                if isinstance(obj, dict):
                    new_value.append(deep_case_transform(obj, to_case_func))
                else:
                    new_value.append(obj)

            transformed_data[transformed_key] = new_value

        if isinstance(value, dict):
            transformed_data[transformed_key] = deep_case_transform(value, to_case_func)

    return transformed_data


def deep_camel_case_transform(data: Dict) -> Dict:
    return deep_case_transform(data, to_camel_case)


def deep_snake_case_transform(data: Dict) -> Dict:
    return deep_case_transform(data, to_snake_case)


class CamelCaseJSONRenderer(renderers.JSONRenderer):
    """
    Converts the default Django REST Framework JSONRenderer from using snake_case
    naming to camelCasing it.
    """

    def render(self, data, *args, **kwargs):
        converted_data = deep_camel_case_transform(data)
        return super().render(converted_data, *args, **kwargs)


class CamelCaseJSONParser(parsers.JSONParser):
    """
    Converts the default Django REST Framework JSONParse from parsing snake_case
    to parsing camelCase and converting it to snake_case.
    """

    def parse(self, stream, *args, **kwargs):
        data = super().parse(stream, *args, **kwargs)
        return deep_snake_case_transform(data)
